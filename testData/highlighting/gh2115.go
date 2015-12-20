package main

type b func(intParam int)
type a b

func c() {
	aFunc := generateFunc()
	if f, ok := aFunc.(a); ok {
		f<error descr="not enough arguments in call to f">()</error>
	}
}

func generateFunc() interface{} {
	return a(func (intParam int) { })
}

func main() {
	c()
}