package com.example1.service.foo.internal;

import com.example1.service.foo.Foo;
import com.example1.service.foo.FooService;
import com.example1.service.foo.internal.bar.Bar;
import com.example1.service.foo.internal.bar.BarHelper;

public class FooServiceImpl implements FooService {
	private final BarHelper barHelper = new BarHelper();

	@Override
	public boolean isFoolish(Foo foo) {
		return foo != null;
	}

	@Override
	public Foo getFoo(String id) {
		Bar bar = this.barHelper.getSomeBar();
		return this.barHelper.transformToFoo(bar);
	}

}
