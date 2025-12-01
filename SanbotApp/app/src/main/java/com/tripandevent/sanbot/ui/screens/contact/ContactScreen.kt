package com.tripandevent.sanbot.ui.screens.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.components.*
import com.tripandevent.sanbot.ui.theme.*

@Composable
fun ContactScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSuccess: (String) -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess && formState.leadId != null) {
            onSuccess(formState.leadId!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        SanbotTopBar(
            title = stringResource(R.string.contact_information),
            onBackClick = onBackClick,
            onHomeClick = onHomeClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.lets_get_in_touch),
                style = MaterialTheme.typography.headlineSmall,
                color = DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            SanbotTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                label = "${stringResource(R.string.your_name)} *",
                isError = formState.nameError != null,
                errorMessage = formState.nameError,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (formState.nameError != null) ErrorRed else OnSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SanbotTextField(
                value = formState.phone,
                onValueChange = { viewModel.updatePhone(it) },
                label = "${stringResource(R.string.phone_number)} *",
                isError = formState.phoneError != null,
                errorMessage = formState.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = if (formState.phoneError != null) ErrorRed else OnSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SanbotTextField(
                value = formState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = stringResource(R.string.email_optional),
                isError = formState.emailError != null,
                errorMessage = formState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = if (formState.emailError != null) ErrorRed else OnSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SanbotTextField(
                value = formState.destination,
                onValueChange = { viewModel.updateDestination(it) },
                label = stringResource(R.string.destination),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = OnSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SanbotTextField(
                    value = formState.numberOfTravelers,
                    onValueChange = { viewModel.updateNumberOfTravelers(it) },
                    label = stringResource(R.string.number_of_travelers),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                        )
                    }
                )

                SanbotTextField(
                    value = formState.travelDate,
                    onValueChange = { viewModel.updateTravelDate(it) },
                    label = stringResource(R.string.travel_date),
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text(stringResource(R.string.additional_notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    unfocusedBorderColor = LightGray,
                    cursorColor = BrandBlue
                )
            )

            if (formState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = ErrorRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = stringResource(R.string.submit),
                onClick = { viewModel.submitForm() },
                enabled = viewModel.isFormValid(),
                isLoading = formState.isSubmitting
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
