package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSubtleBorder
import com.example.util.CountryGroup
import com.example.util.CountryTimezone
import com.example.util.CountryTimezoneHelper

@Composable
fun CountryTimezoneDialog(
  currentCountry: String?,
  currentTimezoneId: String?,
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onSave: (countryName: String?, timezoneId: String?) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCountryGroup by remember { mutableStateOf<CountryGroup?>(null) }
  var selectedTimezone by remember { mutableStateOf<CountryTimezone?>(null) }
  var isSelectingTimezone by remember { mutableStateOf(false) }

  val filteredGroups = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      CountryTimezoneHelper.ALL_COUNTRIES
    } else {
      CountryTimezoneHelper.ALL_COUNTRIES.filter {
        it.countryName.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  val cardBg = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = if (isNightMode) Color(0xFFF0F2F5) else Color(0xFF1A1A1F)
  val secondaryText = if (isNightMode) Color(0xFFA0A6B2) else Color(0xFF8E96A3)
  val inputContainer = if (isNightMode) Color(0xFF252731) else Color(0xFFEEF1F5)
  val saveBtnBg = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val saveBtnText = if (isNightMode) Color(0xFF1A1A1F) else Color.White
  val cancelBtnBg = if (isNightMode) Color(0xFF2E323D) else Color(0xFFEEF1F5)
  val cancelBtnText = if (isNightMode) Color(0xFFF0F2F5) else Color(0xFF1A1A1F)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x73141A26))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onDismiss
      ),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .shadow(
          elevation = 32.dp,
          shape = RoundedCornerShape(24.dp),
          spotColor = Color(0x2E141E32),
          ambientColor = Color.Transparent
        )
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(24.dp),
      color = cardBg,
      border = BorderStroke(1.dp, borderColor)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon Circle Header
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isNightMode) Color(0xFF252731) else Color(0xFFDCE5EE)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Public,
            contentDescription = null,
            tint = if (isNightMode) Color(0xFFDCDDE2) else Color(0xFF3A3A44),
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
          text = if (isSelectingTimezone) "Select Timezone" else "Select Your Country",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        Text(
          text = if (isSelectingTimezone) 
            "Pick the exact region for ${selectedCountryGroup?.countryName ?: ""}" 
            else "Choose location for local date & focus stats",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
          ),
          color = secondaryText,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isSelectingTimezone) {
          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search country...", color = secondaryText, fontSize = 14.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = secondaryText,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear",
                    tint = secondaryText,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("country_timezone_search_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = inputContainer,
              unfocusedContainerColor = inputContainer,
              focusedBorderColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
              unfocusedBorderColor = borderColor,
              focusedTextColor = textColor,
              unfocusedTextColor = textColor,
              cursorColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
              selectionColors = TextSelectionColors(
                handleColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                backgroundColor = (if (isNightMode) Color.White else Color(0xFF1A1A1F)).copy(alpha = 0.2f)
              )
            )
          )

          Spacer(modifier = Modifier.height(12.dp))
        }

        // Searchable Country List or Timezone List
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp)),
          color = if (isNightMode) Color(0xFF14161C) else Color(0xFFF8FAFC),
          border = BorderStroke(1.dp, borderColor)
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxSize().padding(vertical = 6.dp)
          ) {
            if (!isSelectingTimezone) {
              // "Use Device Default" Option
              item {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onSave(null, "DEVICE_DEFAULT") }
                    .padding(horizontal = 14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = if (currentTimezoneId == "DEVICE_DEFAULT" || currentTimezoneId == null) textColor else Color.Transparent,
                    modifier = Modifier.size(18.dp).padding(end = 8.dp)
                  )
                  Text(
                    text = "Use Device Default",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp
                    ),
                    color = textColor
                  )
                }
              }

              items(filteredGroups, key = { it.countryName }) { group ->
                val isSelected = currentCountry == group.countryName

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable {
                      if (group.timezones.size == 1) {
                        onSave(group.countryName, group.timezones.first().timezoneId)
                      } else {
                        selectedCountryGroup = group
                        isSelectingTimezone = true
                      }
                    }
                    .padding(horizontal = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = group.countryName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 15.sp
                    ),
                    color = textColor,
                    modifier = Modifier.weight(1f)
                  )
                  
                  if (group.timezones.size > 1) {
                    Text(
                      text = "${group.timezones.size} zones",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                      color = secondaryText
                    )
                  }
                }
              }
            } else {
              // Timezone selection for multi-timezone country
              selectedCountryGroup?.let { group ->
                items(group.timezones, key = { it.timezoneId }) { tz ->
                  val isSelected = currentTimezoneId == tz.timezoneId

                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(48.dp)
                      .clickable {
                        onSave(group.countryName, tz.timezoneId)
                      }
                      .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = tz.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                          fontSize = 15.sp
                        ),
                        color = textColor
                      )
                      Text(
                        text = tz.formattedOffset,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = secondaryText
                      )
                    }

                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Cancel or Back
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = {
              if (isSelectingTimezone) {
                isSelectingTimezone = false
                selectedCountryGroup = null
              } else {
                onDismiss()
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("country_picker_cancel_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = cancelBtnBg,
              contentColor = cancelBtnText
            )
          ) {
            Text(
              text = if (isSelectingTimezone) "Back" else "Cancel",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
              )
            )
          }

          if (!isSelectingTimezone) {
             Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}
