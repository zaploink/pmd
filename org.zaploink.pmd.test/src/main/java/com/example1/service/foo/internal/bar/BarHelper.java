package com.example1.service.foo.internal.bar;

import com.example1.service.foo.Foo;

public class BarHelper {
	public Bar getSomeBar() {
		return new Bar("baz");
	}

	public Foo transformToFoo(Bar bar) {
		return new Foo(bar.getName());
	}
}
