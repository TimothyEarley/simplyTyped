let pickOne = λ nb . λ b . if b
	then <nat = nb.nat>
	else <bool = nb.bool>
in
(pickOne {nat = 5, bool = true}) false