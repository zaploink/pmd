package com.example1.client;

import com.example1.service.foo.Foo;
import com.example1.service.foo.FooService;
import com.example1.service.foo.internal.bar.Bar;
import com.example1.service.foo.internal.bar.BarHelper;

public class BadUsage1 {
	private FooService fooService;
	private BarHelper barHelper;

	public void useFoo() {
		Foo foo = this.fooService.getFoo("id");
		if (!this.fooService.isFoolish(foo)) {
			// FIXME: access to internal Bar (should not be exposed)
			Bar bar = useBar();
			System.out.println(bar);
		}
		// ...
	}

	public Bar useBar() {
		// FIXME: access to internal barHelper (should not be exposed)
		return this.barHelper.getSomeBar();
	}
}
