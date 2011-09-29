/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.cmisbox.ui;

import java.awt.GraphicsConfiguration;
import java.awt.Shape;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Anthony Petrov
 */
public class AWTUtilitiesWrapper {

	private static Class<?> awtUtilitiesClass;
	private static Class<?> translucencyClass;
	private static Method mIsTranslucencySupported, mIsTranslucencyCapable,
			mSetWindowShape, mSetWindowOpacity, mSetWindowOpaque;
	public static Object PERPIXEL_TRANSPARENT, TRANSLUCENT,
			PERPIXEL_TRANSLUCENT;

	static {
		AWTUtilitiesWrapper.init();
	}

	static void init() {
		try {
			AWTUtilitiesWrapper.awtUtilitiesClass = Class
					.forName("com.sun.awt.AWTUtilities");
			AWTUtilitiesWrapper.translucencyClass = Class
					.forName("com.sun.awt.AWTUtilities$Translucency");
			if (AWTUtilitiesWrapper.translucencyClass.isEnum()) {
				Object[] kinds = AWTUtilitiesWrapper.translucencyClass
						.getEnumConstants();
				if (kinds != null) {
					AWTUtilitiesWrapper.PERPIXEL_TRANSPARENT = kinds[0];
					AWTUtilitiesWrapper.TRANSLUCENT = kinds[1];
					AWTUtilitiesWrapper.PERPIXEL_TRANSLUCENT = kinds[2];
				}
			}
			AWTUtilitiesWrapper.mIsTranslucencySupported = AWTUtilitiesWrapper.awtUtilitiesClass
					.getMethod("isTranslucencySupported",
							AWTUtilitiesWrapper.translucencyClass);
			AWTUtilitiesWrapper.mIsTranslucencyCapable = AWTUtilitiesWrapper.awtUtilitiesClass
					.getMethod("isTranslucencyCapable",
							GraphicsConfiguration.class);
			AWTUtilitiesWrapper.mSetWindowShape = AWTUtilitiesWrapper.awtUtilitiesClass
					.getMethod("setWindowShape", Window.class, Shape.class);
			AWTUtilitiesWrapper.mSetWindowOpacity = AWTUtilitiesWrapper.awtUtilitiesClass
					.getMethod("setWindowOpacity", Window.class, float.class);
			AWTUtilitiesWrapper.mSetWindowOpaque = AWTUtilitiesWrapper.awtUtilitiesClass
					.getMethod("setWindowOpaque", Window.class, boolean.class);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	private static boolean isSupported(Method method, Object kind) {
		if ((AWTUtilitiesWrapper.awtUtilitiesClass == null) || (method == null)) {
			return false;
		}
		try {
			Object ret = method.invoke(null, kind);
			if (ret instanceof Boolean) {
				return ((Boolean) ret).booleanValue();
			}
		} catch (IllegalAccessException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (InvocationTargetException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return false;
	}

	public static boolean isTranslucencyCapable(GraphicsConfiguration gc) {
		return AWTUtilitiesWrapper.isSupported(
				AWTUtilitiesWrapper.mIsTranslucencyCapable, gc);
	}

	public static boolean isTranslucencySupported(Object kind) {
		if (AWTUtilitiesWrapper.translucencyClass == null) {
			return false;
		}
		return AWTUtilitiesWrapper.isSupported(
				AWTUtilitiesWrapper.mIsTranslucencySupported, kind);
	}

	private static void set(Method method, Window window, Object value) {
		if ((AWTUtilitiesWrapper.awtUtilitiesClass == null) || (method == null)) {
			return;
		}
		try {
			method.invoke(null, window, value);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (InvocationTargetException ex) {
			Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	public static void setWindowOpacity(Window window, float opacity) {
		AWTUtilitiesWrapper.set(AWTUtilitiesWrapper.mSetWindowOpacity, window,
				Float.valueOf(opacity));
	}

	public static void setWindowOpaque(Window window, boolean opaque) {
		AWTUtilitiesWrapper.set(AWTUtilitiesWrapper.mSetWindowOpaque, window,
				Boolean.valueOf(opaque));
	}

	public static void setWindowShape(Window window, Shape shape) {
		AWTUtilitiesWrapper.set(AWTUtilitiesWrapper.mSetWindowShape, window,
				shape);
	}
}
