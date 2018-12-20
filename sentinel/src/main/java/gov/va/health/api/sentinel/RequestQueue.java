package gov.va.health.api.sentinel;

/** The RequestQueue holds the Queue utilized by the Crawler. */
public interface RequestQueue {
  String next();

  boolean hasNext();

  void add(String url);
}
