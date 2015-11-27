package com.example2.client;

import com.example2.service.foo.FooService;
import com.example2.storage.foo.FooData;
import com.example2.storage.foo.FooRepository;

public class BadUsage2 {
	private FooService fooService;
	private FooRepository repo;

	public void useFoo() {
		// access to storage class FooData is explicitly permitted (exposed on FooService's API)
		FooData foo = this.fooService.getFoo("id");
		if (!this.fooService.isFoolish(foo)) {
			System.out.println("...");
		}
		// ...
	}

	public void saveFoo(FooData foo) {
		// FIXME: direct access to repository!
		this.repo.store(foo);
	}
}
