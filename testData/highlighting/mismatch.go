package main

type validate interface {
    validate() (bool, error)
    void()
}

func main() {
    var s validate
    <error descr="Assignment count mismatch: 2 element(s) assigned to 1 element(s)">err := <error descr="Multiple-value s.validate() in single-value context">s.validate()</error></error>
    _ = err
    <error descr="Assignment count mismatch: 0 element(s) assigned to 1 element(s)">v := <error descr="s.void() doesn't return a value">s.void()</error></error>
    _ = v
    <error descr="Assignment count mismatch: 0 element(s) assigned to 1 element(s)">shortDeclarationWithoutExpressions :=</error><error descr="<expression> expected, got ';'"> </error>;
    _ = shortDeclarationWithoutExpressions
}