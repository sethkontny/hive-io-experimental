/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.hiveio.conf;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class configuration option
 * @param <C> interface of class
 */
public class ClassConfOption<C> extends AbstractConfOption {
  /** Logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClassConfOption.class);

  /** Base interface for class */
  private final Class<C> interfaceClass;
  /** Default class if not set in configuration */
  private final Class<? extends C> defaultClass;

  /**
   * Private constructor
   * @param key Key
   * @param defaultClass default class
   * @param interfaceClass interface class
   */
  private ClassConfOption(String key, Class<? extends C> defaultClass,
      Class<C> interfaceClass) {
    super(key);
    this.defaultClass = defaultClass;
    this.interfaceClass = interfaceClass;
    AllOptions.add(this);
  }

  /**
   * Static create method
   * @param key key
   * @param defaultClass default class
   * @param interfaceClass interface class
   * @param <T> type of class
   * @return ClassConfOption
   */
  public static <T> ClassConfOption<T> create(String key,
      Class<? extends T> defaultClass, Class<T> interfaceClass) {
    return new ClassConfOption<T>(key, defaultClass, interfaceClass);
  }

  public Class<? extends C> getDefaultClass() {
    return defaultClass;
  }

  public Class<C> getInterfaceClass() {
    return interfaceClass;
  }

  @Override public String getDefaultValueStr() {
    return defaultClass == null ? "null" : defaultClass.getSimpleName();
  }

  @Override public ConfOptionType getType() {
    return ConfOptionType.CLASS;
  }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder(30);
    sb.append("  ");
    sb.append(getKey()).append(" => ").append(getDefaultValueStr());
    sb.append(" [").append(interfaceClass.getSimpleName()).append("] ");
    sb.append(" (").append(getType().toString().toLowerCase()).append(")\n");
    return sb.toString();
  }

  /**
   * Lookup value
   * @param conf Configuration
   * @return Class set for key, or defaultClass
   */
  public Class<? extends C> get(Configuration conf) {
    return conf.getClass(getKey(), defaultClass, interfaceClass);
  }

  /**
   * Lookup array of classes for key
   * @param conf Configuration
   * @return array of classes
   */
  public Class<? extends C>[] getArray(Configuration conf) {
    return getClassesOfType(conf, getKey(), interfaceClass);
  }

  /**
   * Get classes from a property that all implement a given interface.
   *
   * @param conf Configuration
   * @param name String name of property to fetch.
   * @param xface interface classes must implement.
   * @param defaultValue If not found, return this
   * @param <T> Generic type of interface class
   * @return array of Classes implementing interface specified.
   */
  public static <T> Class<? extends T>[] getClassesOfType(Configuration conf,
      String name, Class<T> xface, Class<? extends T> ... defaultValue) {
    Class<?>[] klasses = conf.getClasses(name, defaultValue);
    for (Class<?> klass : klasses) {
      if (!xface.isAssignableFrom(klass)) {
        throw new RuntimeException(klass + " is not assignable from " +
            xface.getName());
      }
    }
    return (Class<? extends T>[]) klasses;
  }

  /**
   * Lookup with user specified default value
   * @param conf Configuration
   * @param defaultValue default value
   * @return Class
   */
  public Class<? extends C> getWithDefault(Configuration conf,
      Class<? extends C> defaultValue) {
    return conf.getClass(getKey(), defaultValue, interfaceClass);
  }

  /**
   * Set value for key
   * @param conf Configuration
   * @param klass Class to set
   */
  public void set(Configuration conf, Class<? extends C> klass) {
    conf.setClass(getKey(), klass, interfaceClass);
  }

  /**
   * Add class to list for key
   * @param conf Configuration
   * @param klass Class to add
   */
  public void add(Configuration conf, Class<? extends C> klass) {
    addToClasses(conf, getKey(), klass, interfaceClass);
  }

  /**
   * Add a class to a property that is a list of classes. If the property does
   * not exist it will be created.
   *
   * @param <T> type of class
   * @param conf Configuration
   * @param name String name of property.
   * @param klass interface of the class being set.
   * @param xface Class to add to the list.
   */
  public static <T> void addToClasses(Configuration conf, String name,
      Class<? extends T> klass, Class<T> xface) {
    if (!xface.isAssignableFrom(klass)) {
      throw new RuntimeException(klass + " does not implement " +
          xface.getName());
    }
    String value;
    String klasses = conf.get(name);
    if (klasses == null) {
      value = klass.getName();
    } else {
      value = klasses + "," + klass.getName();
    }
    conf.set(name, value);
  }
}
