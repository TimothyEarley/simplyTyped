let 1 = succ 0 in
let 2 = succ 1 in
let 3 = succ 2 in
let 4 = succ 3 in
let 5 = succ 4 in

let and = λ a:Bool . λ b:Bool . if a
	then b
	else false
in

letrec equal : Nat -> Nat -> Bool =
	λ a:Nat . λ b:Nat . if iszero a
		then iszero b
		else if iszero b
			then false
			else (equal (pred a)) (pred b)
in

letrec add : Nat -> Nat -> Nat =
	λ a:Nat . λ b:Nat . if iszero a
		then b
		else succ ((add (pred a)) b)
in

letrec times : Nat -> Nat -> Nat =
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

{
	3times4is4times3 = ((equal ((times 3) 4)) ((times 4) 3)),
	facsum = (factorialAndSum 5)
}