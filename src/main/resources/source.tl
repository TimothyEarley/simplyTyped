type BiOp = Nat -> Nat -> Nat in

let and = λ a:Bool . λ b:Bool . if a
	then b
	else false
in

let not = λ b:Bool . if b then false else true in

letrec equal : Nat -> Nat -> Bool =
	λ a:Nat . λ b:Nat . if iszero a
		then iszero b
		else if iszero b
			then false
			else (equal (pred a)) (pred b)
in

letrec add : BiOp =
	λ a:Nat . λ b:Nat . if iszero a
		then b
		else succ ((add (pred a)) b)
in

letrec times : BiOp =
	λ a:Nat . λ b:Nat . if iszero a
		then 0
		else if (equal a) 1
				then b
				else (add b) ((times (pred a)) b)
in

letrec factorialAndSum : Nat -> {factorial : Nat, sum: Nat} = λ n:Nat .
	if (iszero n)
	then {factorial = 1, sum = 0}
	else let prev = factorialAndSum (pred n) in
		{
			factorial = ((times n) (prev.factorial)),
			sum = ((add n) (prev.sum))
		}
in

type NatBool = <nat = Nat, bool = Bool> in

let natOrBool = λ b : Bool . if b
	then <bool = (not true)> as NatBool
	else <nat = 1> as NatBool
in

let backToBool = λ nb : NatBool . case nb of
	  <nat = n> => false
	| <bool = b> => true
in

{
	timesWorks = ((equal ((times 3) 4)) ((times 4) 3)),
	facsum = (factorialAndSum 5),
	u = unit,
	variantTest = (backToBool (natOrBool true)),
	variantTestTwo = (backToBool (natOrBool false)),
}