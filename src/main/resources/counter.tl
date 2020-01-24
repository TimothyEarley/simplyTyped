rectype Counter = {
	value: Nat,
	inc: Unit -> Counter,
	dec: Unit -> Counter
} in

letrec mkCounter : Nat -> Counter = λ v : Nat . fold[Counter] {
	get = v,
	inc = (λ_:Unit . (mkCounter (succ v))),
	dec = (λ_:Unit .  (mkCounter (pred v)))
}
in

(
	mkCounter 0
)
