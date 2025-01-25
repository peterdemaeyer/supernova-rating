# De Maeyer rating system

I started thinking about a rating system before I went looking on the internet and learned about the Elo rating system.
This was on purpose: I like thinking about a problem myself first before looking for a solution on the internet.

The De Maeyer rating system offers a rating system for zero-sum team games such as table soccer and padel.
It has similar properties as the [Elo rating system](https://en.wikipedia.org/wiki/Elo_rating_system), but is sufficiently different to deserve its own name.
I will not explain the Elo rating system here, but while explaining the De Maeyer rating system, I will make a comparison with the Elo rating system. 


## Why not use the Elo rating system?

The Elo rating system was invented for chess.

It is for single-player games.
It is not for team games.
Not without modifications that is.

It is for games with a binary score: 1 for win, 0 for loss, and 0.5 for a draw.
It is not for games with non-binary scores, such as 7-11.
Not without modifications that is.

Its ratings use a logarithmic scale.
There is nothing wrong with that, but why not use a linear scale?
Maybe there are reasons not to, but I decided I wanted to try and design my own system anyway.

It starts by giving unrated players an initial rating which is unrealistic

It has no rating decay mechanism for inactive players.
Not without modifications that is.

When modifying the Elo rating system to suit all the requirements that I have in mind, I could just as well try and design my own rating system.

## Physical meaning of rating

The individual ratings of the players have to have a tangible meaning.
It has to be a measure of the strength of a player.
Two players having a rating of 10 and 1 means that one is 10 times more likely to win a "point" than the other.
The definition of a "point" is a choice, not necessarily to be taken literally, and depends on the game.
In table soccer for example, a point is one goal scored.
In padel for example, a point is one _game_ won.

### Rating in padel

Let's explore the example of padel a bit further.
One could also imagine that a point is one point won _in_ a game.
That would however be impractical, because it cannot be derived from the typical score results.
Score results keep track of games, sets and matches, but not of individual points _in_ every game. 
For that reason, we _choose_ the physical meaning of a "point" being a game won rather than an actual point won.

Tie breaks are special.
The score is counted differently there: in a tie-break, the score _is_ the point difference.
We must be careful not to mix different meanings of a point in a single rating system.
Therefore, a tie-break as a whole counts as one "point" won or lost,
because that corresponds most closely to our chosen meaning that a "point" is a game won.

## Rating scale

The rating scale is not really important.
Whether ratings are in the range of [0, 0.001] or in the range of [0, 1000], it does not really matter.
It may matter for perception though.
For example in the case of padel, given that the official classification (in Belgium) uses a scale of {P50, P100, P200, P300, P400, P500, P700, P1000}, it helps player's intuition if ratings are more or less in that [50, 1000] range.
Therefore, the scale is a configuration parameter.

## Rating update

A rating update happens when there is a match result (score).
The updated rating is a function of the old rating and the new match result.

### De Maeyer rating update

The De Maeyer rating system updates ratings as follows:

$R' = (1 - w) * R + w * R(e, a)$

where
- $R'$ = the updated rating
- $R$ = the initial rating
- $w$ = a weight in the range [0.0, 1.0], saying how much the latest score influences the rating
- $R(e, a)$ = a function that computes a rating update based on the expected score $e$ and the actual score $a$

### Elo rating update

For comparison, the Elo rating system works similarly:

$R' = R + K * (S - E)$

where
- $R'$ = the updated rating
- $R$ = the initial rating
- $K$ = a K-factor in a typical range [10, 40], saying how much the latest score influences the rating
- $S$ = the actual score in the range [0.0, 1.0]
- $E$ = the expected score in the range [0.0, 1.0]

where typically

$E_A = {1 \over {1 + 10^{R_B-R_A \over 400}}}$

where
- $R_A$ = a player's rating
- $R_B$ = an opponent's rating
- $400$ = scaling factor, meaning that a 10x (because logarithmic base is 10) stronger player has a rating 400 higher than the weaker player

## Expected versus actual result (score)

A requirement of a rating system is that it is not based on win or loss, but on win or loss _compared to expectations_.
A weak player that performs better than expected against a strong player should still see their rating increase.

## No inflation (or deflation)

This means that the total amount of rating in the system is constant.
A rating update after a match must therefore result in an exchange of an amount of rating, such that the total amount of rating in the system is unaltered.

Why is this important, you might think?
After all, economy revolves around inflation, so why not a rating system?
One could indeed imagine a rating system without such a restriction.
For example, imagine a system where ratings only go up for winners, and remain fixed (don't go down) for losers.
Then players that haven't played for a while would keep their rating, but would find their rating devalued when returning to play against players that have been playing a lot more.
It would defeat our initial intent, that rating is a measure of a player's strength.
A no-inflation system is what we want.

### Creeping inflation

Both De Maeyer and Elo rating systems are no-inflation rating systems.
However, using the Elo rating system as an example, when a player enters, they contribute an amount of rating to the system.
As they get better over time, their rating will rise above average, and when they retire, they will have a high rating that is never returned to the system.
This phenomenon is a form of creeping inflation.

Rating systems can compensate that by regularly scanning all players, stripping inactive players of their rating, and returning (some of) it to the system's rating pool. 

## Rating pool (or continuum)

It is useful to think of rating as a pool (or continuum) of rating.
Some/most of that rating is kept in the players' ratings.
If there is a shortage or excess, that is kept in the rating system.

### Initial player rating

Given that every player contributes an amount of rating to the pool, that rating is by definition the average rating.
We don't want to give unrated players an initial average rating straight away.
It is much more realistic to give them a portion of their initial rating contribution, and let the rest of that be soaked up by the rating system. 

Assuming that half of the padel population is P100, with P200 being the midpoint - you're P200 if you cross the border between "the lower half" and "the upper half" of players.
Of course, we don't want everyone to start off with a rating of 200 and then have them drop down the first 10 matches or so.
So we give them an initial rating of 75, with 125 being contributed to the continuum.
When they're good, they'll earn it back in no time in their next 1-10 matches.

## Player populations and rating connections

The rating is a measure for a player's strength.
These measures only mean something compared to each other.
Consider two populations of players A and B that never play against each other, but only against players of their own population.
A rating of a player in population A is meaningless compared to that of a player in population B.

For rating to have a global meaning, there has to be a connection between all players.
It doesn't matter if such a connection is first-degree connection or a higher-degree connection.
The bandwidth of a connection determines how fast rating can flow around between players.

## Rating ante

Given the formula of a rating update of all individuals:

$R' = (1 - w) * R + w * R(e, a)$

It is useful to think of a rating update as an _ante_ of each individual to the game.
Given that there is no inflation, the total sum of antes must be $R' = (1 - w) * R + w * R$,
where $w * R$ is a player's _ante_.

All antes combined (potentially complemented with a bit of rating pool ante) make up the pot.
The pot is redistributed amongst the players according to the outcome.
Players that perform better than expected are the winners, they get more back from the pot than the ante they put it.
For players that perform worse than expected, it's the opposite.

### Pool ante

In addition to the individual antes, there is also an amount of pooled rating that may be added to (or subtracted from) the total ante of a match.
It's a way of gradually releasing (or absorbing) global rating corrections by spreading them across all players while their ratings are updates.
Because of the pooled ante, it is theoretically possible that _all_ players ratings increase (or decrease) after a game.
The losers' ratings will increase much less than the winners'.

## Combining ratings

Team games require that individual ratings are combined into a single rating to compute the expected score with.
A rating combiner is an algorithm that computes a combined rating.
A simple rating combiner would take the average of the ratings of all team members.
Some games may be sensitive to a "weakest link" in the team, requiring a rating combiner that gives more weight to the weakest player.
Anyway, you get the idea.

## Splitting ratings

When a rating update is computed for a team, the amount of rating needs to be split amongst the team members.
A rating splitter is an algorithm that computes the individual ratings from a combined rating.
Essentially it means: if a team wins an amount from the pot, how will the winnings be distributed amongst the team members.

The straightforward implementation is that winnings are distributed according to each player's rating, where the rating acts as a weight.

## Weight

The weight $w$ in the De Maeyer rating system is mostly a configured constant parameter, but not quite.
You should not assume that $w$ is the same for all players.
It can differ between players, depending on their maturity.
In order to be more responsive during the first couple of matches, $w$ is higher for unrated or inexperienced players.
It evolves to a common constant as soon as a certain level of experience is reached.

To boost responsiveness in the first matches, let's say the first 10 matches, the weight is such that the scores of all 10 first games have an equal weight of 1/10th in the rating.
After that, the weight becomes a constant 0.1, meaning that the last match counts for 10% and all previous matches combined for 90%.

## Dividing the pot

Given that ratings are relative probabilities that a player wins a point (a game in padel), the expected score is:

$E_A = R_A / R_B$

and the actual score

$A_A = S_A / S_B$

and the score ratio

$T_A = A_A / E_A$

where
- $R_A$ = player A's rating
- $R_B$ = player B (opponent)'s rating
- $S_A$ = player A's score
- $S_B$ = player B's score

We know how much team A anted in the pot, and how much team B anted.
If $T_A$ < 1, it means team 1 lost.
No matter who anted what, team 2 should be going home with more than their ante.
How much more? That is determined by $T_A$.

TODO Complete the explanation.

## Protection against infinity and zero scores

Scores of N - 0 or 0 - N would result in a ratio of either 0 or infinity.
Either of these would mean that one team is infinitely better than the other, which is not realistic and which would result in undesired monopolization of the pot.
All we can say for such a score is that a team did not manage to win any point out of N, but let's give them the benefit of the doubt and assume that _maybe_ they could have one 1 out of N + 1 points.
That means we cap the scores a (N + 1) - 1 and 1 - (N + 1) for the purpose of computing the actual score ratio.
