cmake_minimum_required(VERSION 3.16)
project(wake_on_lan VERSION 1.0 LANGUAGES CXX)

set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)

if(NOT DEFINED INSTALL_DIR)
    set(INSTALL_DIR "bin")
endif()

find_package(QT NAMES Qt5 Qt6 REQUIRED COMPONENTS Gui Widgets Network)
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Gui Widgets Network)

qt_add_executable(wake_on_lan WIN32 MACOSX_BUNDLE
    src/main.cpp
    src/mainwindow.cpp src/mainwindow.h src/mainwindow.ui
)
target_link_libraries(wake_on_lan PUBLIC
    Qt::Core
    Qt::Gui
    Qt::Widgets
    Qt::Network
)

install(TARGETS wake_on_lan
    RUNTIME DESTINATION "${INSTALL_DIR}"
    BUNDLE DESTINATION "${INSTALL_DIR}"
    LIBRARY DESTINATION "${INSTALL_DIR}"
)
