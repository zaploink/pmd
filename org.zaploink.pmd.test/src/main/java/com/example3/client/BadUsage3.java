package com.example3.client;

import com.example3.legacy.access.foo.FooFacade;
import com.example3.legacy.mixed.api.LegacyBar;
import com.example3.legacy.mixed.api.LegacyFoo;
import com.example3.legacy.mixed.impl.LegacyFooImpl;
import com.example3.legacy.util.BarSaver;
import com.example3.legacy.util.FooSaver;

public class BadUsage3 {
	private FooFacade facade;

	public void useFoo() {
		LegacyFoo foo23 = this.facade.getFoo(23);
		// FIXME: do not use FooSaver, use FooFacade instead
		FooSaver.save(foo23);

		// FIXME: never access internal API! this should be done by a domain service or so...
		((LegacyFooImpl) foo23).doInternalStuff();

		// FIXME: don not use BarSaver, we need an additional BarFacade here to hide this
		BarSaver.saveBar(toBar(foo23));
	}

	private LegacyBar toBar(LegacyFoo aFoo) {
		// ...
		return null;
	}
}
