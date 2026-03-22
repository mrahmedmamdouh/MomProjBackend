package com.evelolvetech.routes.admin

import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import com.evelolvetech.service.mom.MomService
import com.google.gson.Gson
import io.ktor.server.routing.*

fun Route.adminRoutes(
    doctorService: DoctorService,
    categoryService: CategoryService,
    productService: ProductService,
    skuOfferService: SkuOfferService,
    momService: MomService,
    gson: Gson
) {
    adminDoctorRoutes(doctorService, gson)
    adminCategoryRoutes(categoryService, gson)
    adminProductRoutes(productService, gson)
    adminSkuOfferRoutes(skuOfferService, gson)
    adminMomRoutes(momService, gson)
}
