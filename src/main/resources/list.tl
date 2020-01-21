type NatList = <
    nil  = Unit,
    cons = {head : Nat, tail : NatList}
> in

let nil = <nil = unit> as NatList
in

let cons = λ head: Nat . λ tail: NatList .
	<cons = {head = head, tail = tail}> as NatList
in

let isNil = λ l: NatList . case l of
	<nil = _> => true
	| <cons = _> => false
in

let no = <cons = {}> as NatList
in

(isNil nil)