/*
 * Copyright 2016, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Provider of name resolvers for name agnostic consumption.
 *
 * <p>Implementations <em>should not</em> throw. If they do, it may interrupt class loading. If
 * exceptions may reasonably occur for implementation-specific reasons, implementations should
 * generally handle the exception gracefully and return {@code false} from {@link #isAvailable()}.
 */
@ExperimentalApi
public abstract class NameResolverProvider extends NameResolver.Factory {
  /**
   * The port number used in case the target or the underlying naming system doesn't provide a
   * port number.
   */
  public static final Attributes.Key<Integer> PARAMS_DEFAULT_PORT =
      NameResolver.Factory.PARAMS_DEFAULT_PORT;

  private static final List<NameResolverProvider> providers
      = load(NameResolverProvider.class.getClassLoader());
  private static final NameResolver.Factory factory = new NameResolverFactory(providers);

  @VisibleForTesting
  static List<NameResolverProvider> load(ClassLoader classLoader) {
    Iterable<NameResolverProvider> candidates;
    if (isAndroid()) {
      candidates = getCandidatesViaHardCoded();
    } else {
      candidates = getCandidatesViaServiceLoader(classLoader);
    }
    List<NameResolverProvider> list = new ArrayList<NameResolverProvider>();
    for (NameResolverProvider current : candidates) {
      if (!current.isAvailable()) {
        continue;
      }
      list.add(current);
    }
    // Sort descending based on priority.
    Collections.sort(list, Collections.reverseOrder(new Comparator<NameResolverProvider>() {
      @Override
      public int compare(NameResolverProvider f1, NameResolverProvider f2) {
        return f1.priority() - f2.priority();
      }
    }));
    return Collections.unmodifiableList(list);
  }

  /**
   * Loads service providers for the {@link NameResolverProvider} service using
   * {@link ServiceLoader}.
   */
  @VisibleForTesting
  public static Iterable<NameResolverProvider> getCandidatesViaServiceLoader(
      ClassLoader classLoader) {
    Iterable<NameResolverProvider> i
        = ServiceLoader.load(NameResolverProvider.class, classLoader);
    // Attempt to load using the context class loader and ServiceLoader.
    // This allows frameworks like http://aries.apache.org/modules/spi-fly.html to plug in.
    if (!i.iterator().hasNext()) {
      i = ServiceLoader.load(NameResolverProvider.class);
    }
    return i;
  }

  /**
   * Load providers from a hard-coded list. This avoids using getResource(), which has performance
   * problems on Android (see https://github.com/grpc/grpc-java/issues/2037). Any provider that may
   * be used on Android is free to be added here.
   */
  @VisibleForTesting
  public static Iterable<NameResolverProvider> getCandidatesViaHardCoded() {
    // Class.forName(String) is used to remove the need for ProGuard configuration. Note that
    // ProGuard does not detect usages of Class.forName(String, boolean, ClassLoader):
    // https://sourceforge.net/p/proguard/bugs/418/
    List<NameResolverProvider> list = new ArrayList<NameResolverProvider>();
    try {
      list.add(create(Class.forName("io.grpc.internal.DnsNameResolverProvider")));
    } catch (ClassNotFoundException ex) {
      // ignore
    }
    return list;
  }

  @VisibleForTesting
  static NameResolverProvider create(Class<?> rawClass) {
    try {
      return rawClass.asSubclass(NameResolverProvider.class).getConstructor().newInstance();
    } catch (Throwable t) {
      throw new ServiceConfigurationError(
          "Provider " + rawClass.getName() + " could not be instantiated: " + t, t);
    }
  }

  /**
   * Returns non-{@code null} ClassLoader-wide providers, in preference order.
   */
  public static List<NameResolverProvider> providers() {
    return providers;
  }

  public static NameResolver.Factory asFactory() {
    return factory;
  }

  @VisibleForTesting
  static NameResolver.Factory asFactory(List<NameResolverProvider> providers) {
    return new NameResolverFactory(providers);
  }

  private static boolean isAndroid() {
    try {
      // Specify a class loader instead of null because we may be running under Robolectric
      Class.forName("android.app.Application", /*initialize=*/ false,
          NameResolverProvider.class.getClassLoader());
      return true;
    } catch (Exception e) {
      // If Application isn't loaded, it might as well not be Android.
      return false;
    }
  }

  /**
   * Whether this provider is available for use, taking the current environment into consideration.
   * If {@code false}, no other methods are safe to be called.
   */
  protected abstract boolean isAvailable();

  /**
   * A priority, from 0 to 10 that this provider should be used, taking the current environment into
   * consideration. 5 should be considered the default, and then tweaked based on environment
   * detection. A priority of 0 does not imply that the provider wouldn't work; just that it should
   * be last in line.
   */
  protected abstract int priority();

  private static class NameResolverFactory extends NameResolver.Factory {
    private final List<NameResolverProvider> providers;

    public NameResolverFactory(List<NameResolverProvider> providers) {
      this.providers = providers;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
      checkForProviders();
      for (NameResolverProvider provider : providers) {
        NameResolver resolver = provider.newNameResolver(targetUri, params);
        if (resolver != null) {
          return resolver;
        }
      }
      return null;
    }

    @Override
    public String getDefaultScheme() {
      checkForProviders();
      return providers.get(0).getDefaultScheme();
    }

    private void checkForProviders() {
      Preconditions.checkState(!providers.isEmpty(),
          "No NameResolverProviders found via ServiceLoader, including for DNS. "
          + "This is probably due to a broken build. If using ProGuard, check your configuration");
    }
  }
}
