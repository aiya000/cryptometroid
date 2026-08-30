package org.cryptomator.domain.usecases.cloud

class BulkThumbnailGenerationCancelToken {

	@Volatile
	var cancelled: Boolean = false
		private set

	fun cancel() {
		cancelled = true
	}
}
