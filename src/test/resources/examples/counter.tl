# 1
rectype Counter = {
	get: Nat,
	inc: Unit -> Counter,
	dec: Unit -> Counter
} in

# fold
letrec mkCounter : Nat -> Counter = λ v : Nat . {
	get = v,
	inc = (λ_:Unit . (mkCounter (succ v))),
	dec = (λ_:Unit .  (mkCounter (pred v)))
}
in

(
	mkCounter 0
).get
#TODO inc.suc.inc
