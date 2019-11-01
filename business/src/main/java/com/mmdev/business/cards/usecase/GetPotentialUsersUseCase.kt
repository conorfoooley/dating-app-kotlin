package com.mmdev.business.cards.usecase

import com.mmdev.business.cards.repository.CardsRepository

/* Created by A on 17.09.2019.*/

/**
 * This is the documentation block about the class
 */

class GetPotentialUsersUseCase (private val repository: CardsRepository) {

	fun execute() = repository.getPotentialCardItems()
}