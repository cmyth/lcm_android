#
# libcmyth example application for Android
#

TOOLS=/Users/gettler/android/android-sdk-mac_x86/tools
PLATFORM=/Users/gettler/android/android-sdk-mac_x86/platform-tools
NDK=/Users/gettler/android/android-ndk-r5b

TARGET=lcm

EMULATOR=emulator-5554

default: debug

debug:
	$(NDK)/ndk-build
	ant debug

clean:
	$(NDK)/ndk-build clean
	ant clean

sim:
	$(PLATFORM)/adb -s $(EMULATOR) install bin/$(TARGET)-debug.apk

sim_update:
	$(PLATFORM)/adb -s $(EMULATOR) install -r bin/$(TARGET)-debug.apk

hw:
	$(PLATFORM)/adb -d install bin/$(TARGET)-debug.apk

hw_update:
	$(PLATFORM)/adb -d install -r bin/$(TARGET)-debug.apk
