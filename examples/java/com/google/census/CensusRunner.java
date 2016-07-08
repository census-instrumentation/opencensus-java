package com.google.census;

public class CensusRunner {
  public static void main(String args[]) {
    System.err.println("Hello Census World");
    TagMap tags = TagMap.of(new TagKey("foo"), "bar", new TagKey("baz"), "bin");
    System.err.println("Tags: " + CensusContext.DEFAULT.with(tags));
  }
}
