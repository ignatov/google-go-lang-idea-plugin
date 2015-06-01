package main

func doubleReturn() (string, string) {
	return "foo", "bar";
}

func main() {
	<error descr="Assignment count mismatch: 2 element(s) assigned to 1 element(s)">_ = <error descr="Multiple-value doubleReturn() in single-value context">doubleReturn()</error></error>

	var existingVar string
	<error descr="Assignment count mismatch: 2 element(s) assigned to 1 element(s)">existingVar = <error descr="Multiple-value doubleReturn() in single-value context">doubleReturn()</error></error>

	println(existingVar)
}