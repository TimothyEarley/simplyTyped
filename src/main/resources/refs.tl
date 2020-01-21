letrec eq : Nat -> Nat -> Bool = λ a:Nat . λb:Nat . if iszero a
	then iszero b
	else (eq (pred a)) (pred b)
in

type Person = {
	age: (Ref Nat),
	alive: (Ref Bool)
} in

let person = λ age : Nat . λ alive : Bool . {
	age = ref age,
	alive = ref alive
} in

let birthday = λ a: Person . let age = a.age in
	age := (succ !age)
in

let die = λ p:Person . let alive = p.alive in alive := false in

letrec liveLife : Person -> Unit = λ p: Person . let age = p.age in
	if (eq !age) 99
		then die p
		else ((birthday p); (liveLife p))
in

let me = (person 20) true in

(liveLife me); me