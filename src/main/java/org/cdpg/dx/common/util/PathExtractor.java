package org.cdpg.dx.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/**
 * Utility for extracting and validating path segments, particularly UUIDs.
 * Provides common validation patterns used across dataplanes.
 */
public class PathExtractor {
  private static final Logger LOGGER = LogManager.getLogger(PathExtractor.class);

  /**
   * Validates if a string is a valid UUID.
   *
   * @param value the string to validate
   * @return true if valid UUID format, false otherwise
   */
  public static boolean isValidUuid(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    try {
      UUID.fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Extracts a UUID from a normalized path at the specified segment index.
   * Path segments are separated by "/" characters.
   *
   * @param normalizedPath the normalized path (e.g., "/collections/abc-123/items")
   * @param segmentIndex   the zero-based index of the segment to extract
   * @return the UUID as a string
   * @throws IllegalArgumentException if the segment is invalid or not a UUID
   */
  public static String extractAndValidateUuid(String normalizedPath, int segmentIndex) {
    String[] segments = normalizedPath.split("/");

    if (segmentIndex < 0 || segmentIndex >= segments.length) {
      throw new IllegalArgumentException(
          "Segment index " + segmentIndex + " out of bounds for path: " + normalizedPath);
    }

    String segment = segments[segmentIndex];

    if (!isValidUuid(segment)) {
      throw new IllegalArgumentException(
          "Segment at index " + segmentIndex + " is not a valid UUID: " + segment);
    }

    return segment;
  }
}
