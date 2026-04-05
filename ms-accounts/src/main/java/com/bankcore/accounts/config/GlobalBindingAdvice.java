package com.bankcore.accounts.config;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * {@code GlobalBindingAdvice} is a Spring {@link
 * org.springframework.web.bind.annotation.ControllerAdvice} component that customizes the binding
 * behavior for request parameters across all controllers.
 *
 * <p>This advice registers custom editors to handle common binding scenarios:
 *
 * <ul>
 *   <li>Trims incoming {@link String} values and converts empty strings ("") to {@code null} using
 *       {@link org.springframework.beans.propertyeditors.StringTrimmerEditor}.
 *   <li>Handles conversion of {@link Integer} values, allowing empty strings ("") to be treated as
 *       {@code null} using {@link org.springframework.beans.propertyeditors.CustomNumberEditor}.
 * </ul>
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Ensure consistent handling of empty string query parameters across the application.
 *   <li>Prevent issues where empty strings would otherwise cause binding errors.
 *   <li>Centralize binding logic for maintainability and reuse.
 * </ul>
 *
 * <p>Usage Example:
 *
 * <pre>{@code
 * // Example: GET /transactions?page=&size=
 * // Without this advice, empty query params ("") may cause binding errors.
 * // With this advice, they are converted to null, allowing default values in getters.
 * }</pre>
 *
 * @author Bankcore Team
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@ControllerAdvice
public class GlobalBindingAdvice {

  /**
   * Initializes the {@link WebDataBinder} with custom editors for String and Integer types.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>Trim strings and convert empty values to {@code null}.
   *   <li>Allow empty numeric values to be treated as {@code null}.
   * </ul>
   *
   * @param binder the data binder used for binding request parameters
   */
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
  }
}
