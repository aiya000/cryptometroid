package org.cryptomator.domain.usecases.cloud;

import org.cryptomator.domain.CloudFile;

public interface BulkThumbnailGenerationState extends ProgressState {
	CloudFile file();
}