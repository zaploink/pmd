package com.example2.storage.foo;

public interface FooRepository {
	public void store(FooData fooData);

	public FooData load(String id);
}
