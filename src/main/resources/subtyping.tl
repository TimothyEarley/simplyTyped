type Animal = {
	age : Nat,
	alive : Bool
} in

type Bird = {
	age : Nat,
	alive : Bool,
	yellow : Bool
} in

let tweety = {
	age = 5,
	alive = true,
	yellow = true
} in

let isAlive = λ b : Animal . (b.alive)
in

(isAlive tweety)
