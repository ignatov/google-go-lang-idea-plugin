package foo

func demo() (int, int) {
    return 1, 1
}

func main() {
    a, <caret>_ := demo()
    _ = a
}