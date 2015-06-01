package docker7

type Context interface {
	Value(key interface{}) interface{}
}

func _(ctx Context) (val interface{}) {
	val = ctx.Value("demo")
	return
}

type Value struct{}

func (v Value) Zero() Value {
	return v
}

func (v Value) Interface() (a interface{}) {
	return
}

func _(val Value) (v int) {
	v = val.Zero().Interface()
	return v
}
