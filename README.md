#levitha
Levitha, a toolkit providing a collection of composable coordination abstractions on top of a Paxos derived consensus algorithm.

This repository contains the implementation of the Leader Election abstraction, the Group Membership abstraction and the Consensus Abstraction based on top of the State Based Paxos algorithm.

We present the details of the State Based Paxos algorithm in the namesake paper Nicolo' Rivetti and Angelo Corsaro, State Based Paxos,Proceedings of the Industrial Track of the 13th ACM/IFIP/USENIX International Middleware Conference, Middleware, 2013.

Abstract:
Paxos is an algorithm that provides an elegant and optimal solution to the consensus problem in distributed systems. Despite its conceptual simplicity, industrial strength and high performance implementations of Paxos are very hard. This paper presents and evaluates the performance of State Paxos, a novel variation of the Paxos consensus algorithm that exploits overwrite semantics to eliminate most of the complexities and inefficiencies introduced by state management. This variation is suitable in applications where the current state depends only on the last update as opposed to the entire history, such as group management and distributed key-value stores.


