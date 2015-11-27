package com.example3.legacy.access.foo;

import com.example3.legacy.mixed.api.LegacyFoo;

public interface FooFacade {
	public LegacyFoo getFoo(int id);

	public boolean checkFoo(LegacyFoo foo);

	public void save(LegacyFoo foo);
}
