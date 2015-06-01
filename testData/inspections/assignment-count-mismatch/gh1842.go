package main

func Foo() (int, int) {
	a, b := 0, 0
	a, b = 1, 1
	a, b <error descr="Syntax error: unexpected op=, expecting := or = or comma">+=</error> 1, 1
	a, b <error descr="Syntax error: unexpected op=, expecting := or = or comma">/=</error> 1, 1
	a, b <error descr="Syntax error: unexpected op=, expecting := or = or comma">*=</error> 1, 1
	return a, b
}