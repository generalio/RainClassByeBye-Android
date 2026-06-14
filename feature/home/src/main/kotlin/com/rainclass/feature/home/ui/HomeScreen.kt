package com.rainclass.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rainclass.core.config.designsystem.component.ErrorMessage
import com.rainclass.core.config.designsystem.component.LoadingIndicator
import com.rainclass.core.config.designsystem.component.RainClassTopBar
import com.rainclass.feature.home.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  modifier: Modifier = Modifier,
  onNavigateToSettings: () -> Unit,
  onLogout: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.authExpired) {
    if (uiState.authExpired) onLogout()
  }

  Scaffold(
    modifier = modifier,
    topBar = { RainClassTopBar(title = "个人") }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(
          uiState.error!!,
          onRetry = { viewModel.loadUserInfo() }
        )
        uiState.user != null -> {
          val user = uiState.user!!

          ProfileHeader(
            name = user.name.ifBlank { "RainClass 用户" },
            school = user.school,
            schoolNumber = user.schoolNumber
          )

          HomeMenuItem(
            icon = Icons.Default.Settings,
            title = "设置",
            subtitle = "配置 AI 模型和 API Key",
            onClick = onNavigateToSettings
          )

          Spacer(modifier = Modifier.weight(1f))

          OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.error
            )
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("退出登录")
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileHeader(
  name: String,
  school: String,
  schoolNumber: String
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    )
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.AccountCircle,
          contentDescription = null,
          modifier = Modifier.size(52.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = school.ifBlank { "未获取学校信息" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ProfileInfoRow(label = "姓名", value = name)
        ProfileInfoRow(label = "学号", value = schoolNumber.ifBlank { "未获取" })
        ProfileInfoRow(label = "学校", value = school.ifBlank { "未获取" })
      }
    }
  }
}

@Composable
private fun ProfileInfoRow(
  label: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
  }
}

@Composable
private fun HomeMenuItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        icon, contentDescription = null,
        modifier = Modifier.size(40.dp),
        tint = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Icon(
        Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
