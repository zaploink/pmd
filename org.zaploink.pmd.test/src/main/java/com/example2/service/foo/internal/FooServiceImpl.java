package com.example2.service.foo.internal;

import com.example2.service.foo.FooService;
import com.example2.storage.foo.FooData;
import com.example2.storage.foo.FooRepository;

public class FooServiceImpl implements FooService {

	private FooRepository repo;

	@Override
	public boolean isFoolish(FooData foo) {
		return foo != null;
	}

	@Override
	public FooData getFoo(String id) {
		return this.repo.load(id);
	}
}
