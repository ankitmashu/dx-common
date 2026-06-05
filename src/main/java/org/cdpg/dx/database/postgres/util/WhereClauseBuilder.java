package org.cdpg.dx.database.postgres.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for constructing SQL WHERE clauses with parameter binding.
 *
 * Supports dynamic WHERE clause assembly with null-safe conditions,
 * maintaining parameter order for prepared statements.
 *
 * Usage:
 * <pre>
 * WhereClauseBuilder builder = new WhereClauseBuilder();
 * builder.addCondition("status = ?", "active")
 *        .addCondition("age > ?", 18)
 *        .addCondition("name LIKE ?", "%search%");
 * String whereClause = builder.build();  // "WHERE status = ? AND age > ? AND name LIKE ?"
 * Object[] params = builder.getParameters();
 * </pre>
 */
public class WhereClauseBuilder {
  private final List<String> conditions = new ArrayList<>();
  private final List<Object> parameters = new ArrayList<>();

  /**
   * Add a condition to the WHERE clause with an optional parameter.
   *
   * @param condition the condition (e.g., "status = ?")
   * @param parameter the parameter value, or null to skip binding
   * @return this builder for chaining
   */
  public WhereClauseBuilder addCondition(String condition, Object parameter) {
    if (condition != null && !condition.isEmpty()) {
      conditions.add(condition);
      if (parameter != null) {
        parameters.add(parameter);
      }
    }
    return this;
  }

  /**
   * Add a raw condition without parameters.
   *
   * @param condition the raw condition (e.g., "age IS NOT NULL")
   * @return this builder for chaining
   */
  public WhereClauseBuilder addRawCondition(String condition) {
    if (condition != null && !condition.isEmpty()) {
      conditions.add(condition);
    }
    return this;
  }

  /**
   * Alias for {@link #addRawCondition(String)}.
   * Adds a raw condition without parameters.
   *
   * @param condition the raw condition string
   * @return this builder for chaining
   */
  public WhereClauseBuilder add(String condition) {
    return addRawCondition(condition);
  }

  /**
   * Conditionally add a raw condition if the given predicate is true.
   *
   * @param predicate whether to add the condition
   * @param condition the raw condition string
   * @return this builder for chaining
   */
  public WhereClauseBuilder addIf(boolean predicate, String condition) {
    if (predicate) {
      return addRawCondition(condition);
    }
    return this;
  }

  /**
   * Build the WHERE clause string.
   *
   * @return "WHERE condition1 AND condition2..." or empty string if no conditions
   */
  public String build() {
    if (conditions.isEmpty()) {
      return "";
    }
    return "WHERE " + String.join(" AND ", conditions);
  }

  /**
   * Get all parameters in order for prepared statement binding.
   *
   * @return array of parameters
   */
  public Object[] getParameters() {
    return parameters.toArray();
  }

  /**
   * Get the number of conditions in the WHERE clause.
   *
   * @return condition count
   */
  public int getConditionCount() {
    return conditions.size();
  }

  /**
   * Check if this builder has any conditions.
   *
   * @return true if at least one condition exists
   */
  public boolean isEmpty() {
    return conditions.isEmpty();
  }
}
