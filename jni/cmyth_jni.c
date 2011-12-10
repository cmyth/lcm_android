/*
 *  Copyright (C) 2011, Jon Gettler <gettler@mvpmc.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

#include <sys/types.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

#include <jni.h>
#include <android/log.h>

#include <cmyth/cmyth.h>

#define FUNC(x) Java_org_mvpmc_android_lcm_cmyth_libcmyth_##x

#define TAG	"cmyth_jni"

#if 0
#define LOG(x...)	__android_log_print(ANDROID_LOG_DEBUG, TAG, x)
#else
#define LOG(x...)
#endif

typedef unsigned int pointer;

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
//	cmyth_dbg_level(CMYTH_DBG_ALL);
//	refmem_dbg_all();

	return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{
}

void
FUNC(refRelease)(JNIEnv *env, jobject thiz,
		 jlong ptr)
{
	ref_release((void*)(pointer)ptr);
}

void
FUNC(refHold)(JNIEnv *env, jobject thiz,
	      jlong ptr)
{
	ref_hold((void*)(pointer)ptr);
}

void
FUNC(refDebug)(JNIEnv *env, jobject thiz)
{
	__android_log_print(ANDROID_LOG_DEBUG, "jni", "refDebug()");
	ref_alloc_show();
}

jlong
FUNC(connect)(JNIEnv* env, jobject thiz,
	      jstring server, jint port, jlong buflen, jlong tcp_rcvbuf)
{
	cmyth_conn_t control;
	char *host = (char*)(*env)->GetStringUTFChars(env, server, 0);

	if ((control=cmyth_conn_connect_ctrl(host, port,
					     buflen, tcp_rcvbuf)) == NULL) {
		return 0;
	}

	return (jlong)(pointer)control;
}

jlong
FUNC(connectEvent)(JNIEnv* env, jobject thiz,
		   jlong prog, jlong control, jlong buflen, jlong tcp_rcvbuf)
{
	return 0;
}


jlong
FUNC(connectFile)(JNIEnv* env, jobject thiz,
		  jlong prog, jlong control, jlong buflen, jlong tcp_rcvbuf)
{
	cmyth_file_t f = cmyth_conn_connect_file((cmyth_proginfo_t)(pointer)prog,
						 (cmyth_conn_t)(pointer)control,
						 buflen, tcp_rcvbuf);

	return (jlong)(pointer)f;
}

jint
FUNC(getProtocolVersion)(JNIEnv *env, jobject thiz, long conn)
{
	return cmyth_conn_get_protocol_version((cmyth_conn_t)conn);
}

jlong
FUNC(getAllRecorded)(JNIEnv *env, jobject thiz,
		     jlong conn)
{
	return (pointer)cmyth_proglist_get_all_recorded((cmyth_conn_t)(pointer)conn);
}

jlong
FUNC(proglistGetCount)(JNIEnv *env, jobject thiz,
		       jlong progs)
{
	return cmyth_proglist_get_count((cmyth_proglist_t)(pointer)progs);
}

jlong
FUNC(getProginfo)(JNIEnv *env, jobject thiz,
		  jlong proglist, jint which)
{
	return (pointer)cmyth_proglist_get_item((cmyth_proglist_t)(pointer)proglist, which);
}

jstring
FUNC(getProginfoTitle)(JNIEnv *env, jobject thiz,
		       jlong proginfo)
{
	char *str = cmyth_proginfo_title((cmyth_proginfo_t)(pointer)proginfo);
	jstring js;

	js = (*env)->NewStringUTF(env, str);

	ref_release(str);

	return js;
}

jstring
FUNC(getProginfoSubtitle)(JNIEnv *env, jobject thiz,
			  jlong proginfo)
{
	char *str = cmyth_proginfo_subtitle((cmyth_proginfo_t)(pointer)proginfo);
	jstring js;

	js = (*env)->NewStringUTF(env, str);

	ref_release(str);

	return js;
}

jstring
FUNC(getProginfoHost)(JNIEnv *env, jobject thiz,
		      jlong proginfo)
{
	char *str = cmyth_proginfo_host((cmyth_proginfo_t)(pointer)proginfo);
	jstring js;

	js = (*env)->NewStringUTF(env, str);

	ref_release(str);

	return js;
}

jint
FUNC(getProginfoPort)(JNIEnv *env, jobject thiz,
		      jlong proginfo)
{
	return cmyth_proginfo_port((cmyth_proginfo_t)(pointer)proginfo);
}

jlong
FUNC(getProginfoLength)(JNIEnv *env, jobject thiz,
			jlong proginfo)
{
	return cmyth_proginfo_length((cmyth_proginfo_t)(pointer)proginfo);
}

jstring
FUNC(getProginfoFilename)(JNIEnv *env, jobject thiz,
			  jlong proginfo)
{
	char *str = cmyth_proginfo_pathname((cmyth_proginfo_t)(pointer)proginfo);
	jstring js;

	js = (*env)->NewStringUTF(env, str);

	ref_release(str);

	return js;
}

jlong
FUNC(fileSeek)(JNIEnv *env, jobject thiz,
	       jlong file, jlong offset)
{
	return cmyth_file_seek((cmyth_file_t)(pointer)file, offset, SEEK_SET);
}

jint
FUNC(fileRequestBlock)(JNIEnv *env, jobject thiz,
		       jlong file, jlong size)
{
	int ret;
	unsigned long len = (unsigned long)size;

	ret = cmyth_file_request_block((cmyth_file_t)(pointer)file, len);

	return (jint)ret;
}

jint
FUNC(fileGetBlock)(JNIEnv *env, jobject thiz,
		   jlong file, jbyteArray array, jlong size)
{
	int ret;
	char *buf = NULL;
	unsigned long len = (unsigned long)size;

	buf = (*env)->GetDirectBufferAddress(env, array);

	ret = cmyth_file_get_block((cmyth_file_t)(pointer)file, buf, len);

	return (jint)ret;
}
