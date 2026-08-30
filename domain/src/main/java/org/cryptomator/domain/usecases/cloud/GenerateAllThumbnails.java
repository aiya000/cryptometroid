package org.cryptomator.domain.usecases.cloud;

import org.cryptomator.domain.CloudFolder;
import org.cryptomator.domain.exception.BackendException;
import org.cryptomator.domain.repository.CloudContentRepository;
import org.cryptomator.domain.usecases.ProgressAware;
import org.cryptomator.generator.Parameter;
import org.cryptomator.generator.UseCase;

@UseCase
public class GenerateAllThumbnails {

	private final CloudContentRepository cloudContentRepository;
	private final CloudFolder folder;

	public GenerateAllThumbnails(CloudContentRepository cloudContentRepository, //
			@Parameter CloudFolder folder) {
		this.cloudContentRepository = cloudContentRepository;
		this.folder = folder;
	}

	public void execute(ProgressAware<BulkThumbnailGenerationState> progressAware) throws BackendException {
		timber.log.Timber.d("GenerateAllThumbnails.execute called for folder: " + folder.getName());
		timber.log.Timber.d("CloudContentRepository type: " + cloudContentRepository.getClass().getName());
		
		// Simply call the interface method - let the implementation handle it
		timber.log.Timber.d("Calling cloudContentRepository.generateAllThumbnails...");
		cloudContentRepository.generateAllThumbnails(folder, progressAware);
		timber.log.Timber.d("GenerateAllThumbnails.execute completed");
	}
}