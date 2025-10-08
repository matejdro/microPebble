package com.matejdro.micropebble.ui.debugging

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Expanded [Preview] annotation that displays everything in:
 * * Portrait phone
 * * Landscape phone
 * * Portrait phone in night mode
 * * Landscape phone in night mode
 */
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_YES,
   device = "spec:id=reference_phone,shape=Normal,width=411dp,height=891dp,dpi=420",
   name = "Night mode portrait"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_NO,
   device = "spec:id=reference_phone,shape=Normal,width=411dp,height=891dp,dpi=420",
   name = "Day mode portrait"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_YES,
   device = "spec:id=reference_phone,shape=Normal,width=891dp,height=411dp,dpi=420",
   name = "Night mode landscape"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_NO,
   device = "spec:id=reference_phone,shape=Normal,width=891dp,height=411dp,dpi=420",
   name = "Day mode landscape"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_YES,
   device = "spec:id=reference_phone,shape=Normal,width=300dp,height=600dp,dpi=420",
   name = "Small night mode portrait"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_NO,
   device = "spec:id=reference_phone,shape=Normal,width=300dp,height=600dp,dpi=420",
   name = "Small day mode portrait"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_YES,
   device = "spec:id=reference_phone,shape=Normal,width=600dp,height=300dp,dpi=420",
   name = "Small night mode landscape"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_NO,
   device = "spec:id=reference_phone,shape=Normal,width=600dp,height=300dp,dpi=420",
   name = "Small day mode landscape"
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_NO,
   device = "spec:id=reference_phone,shape=Normal,width=411dp,height=891dp,dpi=420",
   name = "Day mode portrait with large font",
   fontScale = 1.5f
)
@Preview(
   showSystemUi = true,
   uiMode = Configuration.UI_MODE_NIGHT_YES,
   device = "spec:id=reference_phone,shape=Normal,width=891dp,height=411dp,dpi=420",
   name = "Night mode landscape with large font",
   fontScale = 1.5f
)
annotation class FullScreenPreviews
