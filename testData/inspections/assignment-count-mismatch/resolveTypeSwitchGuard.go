package assignmentCount

func _ () {
	type dumpTest struct {
		Body interface{}
	}
	var dumpTests []dumpTest
	for _, tt := range dumpTests {
		func() {
			switch b := tt.Body.(type) {
			case []byte:
				tt.Body = b
			case func() int:
				tt.Body = b()
			}
		}()
	}
}
