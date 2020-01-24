type MaybeNat = <empty = Unit, just = Nat> in

rectype NatList = <
    nil  = Unit,
    cons = {head : Nat, tail : NatList}
> in
type MaybeNatList = <empty = Unit, just = NatList> in

let nil = <nil = unit> as NatList
in

let cons = λ head: Nat . λ tail: NatList .
	<cons = {head = head, tail = tail}> as NatList
in

let isNil = λ l: NatList . case l of
	<nil = _> => true
	| <cons = _> => false
in

let hd = λ l: NatList . case l of
	<nil = _> => <empty = unit> as MaybeNat
	| <cons = p> => <just = p.head> as MaybeNat
in

let tl = λ l: NatList . case l of
	<nil = _> => <empty = unit> as MaybeNatList
	| <cons = p> => <just = p.tail> as MaybeNatList
in

letrec append : Nat -> NatList -> NatList = λ n : Nat . λ l : NatList . case l of
	<nil = _> => (cons n) nil
	| <cons = p> => (cons (p.head)) ((append n) (p.tail))
in

letrec reverse : NatList -> NatList = λ l: NatList . case l of
	<nil = _> => nil
	| <cons = p> => (
		(append (p.head))
		(reverse (p.tail))
	)
in

reverse ((cons 1) ((cons 2) ((cons 3) nil)))