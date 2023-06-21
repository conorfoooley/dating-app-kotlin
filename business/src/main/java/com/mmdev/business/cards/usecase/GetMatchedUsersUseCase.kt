package com.mmdev.business.cards.usecase

import com.mmdev.business.cards.repository.CardsRepository

/* Created by A on 29.10.2019.*/

/**
 * This is the documentation block about the class
 */

class GetMatchedUsersUseCase (private val repository: CardsRepository)  {

	fun execute() = repository.getMatchedUserItems()

}