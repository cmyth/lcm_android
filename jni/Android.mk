# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LIBREFMEM = cmyth/librefmem
LIBCMYTH = cmyth/libcmyth

LOCAL_C_INCLUDES := \
	$(NDK_APP_PROJECT_PATH)/jni/cmyth/include \
	$(NDK_APP_PROJECT_PATH)/jni/cmyth/librefmem \
	$(NDK_APP_PROJECT_PATH)/jni/cmyth/libcmyth

LOCAL_MODULE    := cmyth
LOCAL_SRC_FILES := cmyth_jni.c \
	$(LIBREFMEM)/alloc.c $(LIBREFMEM)/debug.c \
	$(LIBCMYTH)/bookmark.c $(LIBCMYTH)/commbreak.c \
	$(LIBCMYTH)/connection.c $(LIBCMYTH)/debug.c $(LIBCMYTH)/event.c \
	$(LIBCMYTH)/file.c $(LIBCMYTH)/freespace.c $(LIBCMYTH)/keyframe.c \
	$(LIBCMYTH)/livetv.c \
	$(LIBCMYTH)/posmap.c $(LIBCMYTH)/proginfo.c \
	$(LIBCMYTH)/proglist.c $(LIBCMYTH)/rec_num.c $(LIBCMYTH)/recorder.c \
	$(LIBCMYTH)/ringbuf.c $(LIBCMYTH)/socket.c $(LIBCMYTH)/timestamp.c

#	$(LIBCMYTH)/mythtv_mysql.c \
#	$(LIBCMYTH)/mysql_query.c \

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 

include $(BUILD_SHARED_LIBRARY)
