# NanoBase
> A persistent, fault-tolerant, high-performance key/value store

## Description
It's the simplest implementation of a typical distributed system. Several subsystems were designed to 
make it work properly. The store system supported `Comparable` key types and various value types
(however, blob is not implemented currently, so storing small-scale data is accepted). 

The project is written in IntelliJ IDEA 14.1, using the built-in **Maven** controlling system. As the file
`pom.xml` assigned, only **netty 4.x** (for low-level socket I/O) and **junit 4.12** (for unit tests) are needed.
Other components are written by my hand.

## The MIT License (MIT)
```
Copyright (C) 2015 Ren Feng.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
