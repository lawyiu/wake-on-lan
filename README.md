# Wake On LAN
[![CI/CD for Windows](https://github.com/lawyiu/wake-on-lan/actions/workflows/build_test_release.yml/badge.svg)](https://github.com/lawyiu/wake-on-lan/actions/workflows/build_test_release.yml)

An application to send magic packets to start up or wake up computers over a local area network.

## Build Instructions
1. `git clone https://github.com/lawyiu/wake-on-lan.git`
2. `cd wake-on-lan`
3. `mkdir build && cd build`
4. `cmake ..` or `qmake ..`
5. `msbuild wake_on_lan.sln` for cmake or `nmake` for qmake
