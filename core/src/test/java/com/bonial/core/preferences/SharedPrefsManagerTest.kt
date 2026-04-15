package com.bonial.core.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SharedPrefsManagerTest {
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private val mockContext: Context = mock()
    private val mockSharedPreferences: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()

    @Before
    fun setUp() {
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        sharedPrefsManager = SharedPrefsManager(mockContext)
    }

    @Test
    fun `setValue should store a string`() {
        sharedPrefsManager.setValue("key_string", "test_value")
        verify(mockEditor).putString("key_string", "test_value")
        verify(mockEditor).commit()
    }

    @Test
    fun `getStringValue should retrieve a string`() {
        whenever(mockSharedPreferences.getString("key_string", "default")).thenReturn("retrieved_value")
        val result = sharedPrefsManager.getStringValue("key_string", "default")
        assertThat(result).isEqualTo("retrieved_value")
    }

    @Test
    fun `saveObject should store a custom object`() {
        val testObject = TestData("test_name", 25)
        val json = Gson().toJson(testObject)
        sharedPrefsManager.saveObject("key_object", testObject)
        verify(mockEditor).putString("key_object", json)
        verify(mockEditor).commit()
    }

    @Test
    fun `getObject should retrieve a custom object`() {
        val testObject = TestData("test_name", 25)
        val json = Gson().toJson(testObject)
        whenever(mockSharedPreferences.getString(anyOrNull(), anyOrNull())).thenReturn(json)

        val result = sharedPrefsManager.getObject("key_object", TestData::class.java)

        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("test_name")
        assertThat(result?.age).isEqualTo(25)
    }

    @Test
    fun `clear should remove all preferences`() {
        sharedPrefsManager.clear()
        verify(mockEditor).clear()
        verify(mockEditor).commit()
    }

    @Test
    fun `removeKey should remove a specific preference`() {
        sharedPrefsManager.removeKey("some_key")
        verify(mockEditor).remove("some_key")
        verify(mockEditor).commit()
    }

    private data class TestData(
        val name: String,
        val age: Int,
    )
}
