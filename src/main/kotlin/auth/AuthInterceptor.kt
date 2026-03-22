package com.evelolvetech.auth

import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.util.Constants
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.momRoute(path: String, momService: MomService, build: Route.() -> Unit) {
    route(path) {
        authenticate(optional = false) {
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                val userType = principal?.payload?.getClaim("userType")?.asString()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (principal == null || userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNAUTHORIZED_ACCESS
                        )
                    )
                    finish()
                    return@intercept
                }
                
                if (userType != "MOM") {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Access denied: Mom privileges required"
                        )
                    )
                    finish()
                    return@intercept
                }
                
                val mom = momService.getMomById(userId)
                if (mom == null || !mom.isAuthorized) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Mom access has been revoked or is not authorized"
                        )
                    )
                    finish()
                    return@intercept
                }
                proceed()
            }
            build()
        }
    }
}

fun Route.momRoute(momService: MomService, build: Route.() -> Unit) {
    authenticate(optional = false) {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            val userType = principal?.payload?.getClaim("userType")?.asString()
            val userId = principal?.payload?.getClaim("userId")?.asString()
            if (principal == null || userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.UNAUTHORIZED_ACCESS
                    )
                )
                finish()
                return@intercept
            }
            
            if (userType != "MOM") {
                call.respond(
                    HttpStatusCode.Forbidden,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Access denied: Mom privileges required"
                    )
                )
                finish()
                return@intercept
            }
            
            val mom = momService.getMomById(userId)
            if (mom == null || !mom.isAuthorized) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Mom access has been revoked or is not authorized"
                    )
                )
                finish()
                return@intercept
            }
            proceed()
        }
        build()
    }
}

fun Route.momRouteBasic(momService: MomService, build: Route.() -> Unit) {
    authenticate(optional = false) {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            val userType = principal?.payload?.getClaim("userType")?.asString()
            val userId = principal?.payload?.getClaim("userId")?.asString()
            if (principal == null || userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.UNAUTHORIZED_ACCESS
                    )
                )
                finish()
                return@intercept
            }
            
            if (userType != "MOM") {
                call.respond(
                    HttpStatusCode.Forbidden,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Access denied: Mom privileges required"
                    )
                )
                finish()
                return@intercept
            }
            
            proceed()
        }
        build()
    }
}

fun Route.adminRoute(build: Route.() -> Unit) {
    authenticate(optional = false) {
        intercept(ApplicationCallPipeline.Call) {
            
            val principal = call.principal<JWTPrincipal>()
            val userType = principal?.payload?.getClaim("userType")?.asString()
            val userId = principal?.payload?.getClaim("userId")?.asString()
            if (principal == null || userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.UNAUTHORIZED_ACCESS
                    )
                )
                finish()
                return@intercept
            }
            
            if (userType != "ADMIN") {
                call.respond(
                    HttpStatusCode.Forbidden,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Access denied: Admin privileges required"
                    )
                )
                finish()
                return@intercept
            }
            proceed()
        }
        build()
    }
}

fun Route.adminRoute(path: String, build: Route.() -> Unit) {
    route(path) {
        authenticate(optional = false) {
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                val userType = principal?.payload?.getClaim("userType")?.asString()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (principal == null || userType != "ADMIN" || userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNAUTHORIZED_ACCESS
                        )
                    )
                    finish()
                    return@intercept
                }
                proceed()
            }
            build()
        }
    }
}

fun Route.doctorRoute(path: String, doctorService: com.evelolvetech.service.doctor.DoctorService, build: Route.() -> Unit) {
    route(path) {
        authenticate(optional = false) {
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                val userType = principal?.payload?.getClaim("userType")?.asString()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (principal == null || userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNAUTHORIZED_ACCESS
                        )
                    )
                    finish()
                    return@intercept
                }
                
                if (userType != "DOCTOR") {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Access denied: Doctor privileges required"
                        )
                    )
                    finish()
                    return@intercept
                }

                val doctor = doctorService.getDoctorById(userId)
                if (doctor == null || !doctor.isAuthorized) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Doctor access has been revoked or is not authorized"
                        )
                    )
                    finish()
                    return@intercept
                }

                proceed()
            }
            build()
        }
    }
}

fun Route.doctorRouteBasic(doctorService: com.evelolvetech.service.doctor.DoctorService, build: Route.() -> Unit) {
    authenticate(optional = false) {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            val userType = principal?.payload?.getClaim("userType")?.asString()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            if (principal == null || userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.UNAUTHORIZED_ACCESS
                    )
                )
                finish()
                return@intercept
            }
            
            if (userType != "DOCTOR") {
                call.respond(
                    HttpStatusCode.Forbidden,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Access denied: Doctor privileges required"
                    )
                )
                finish()
                return@intercept
            }

            proceed()
        }
        build()
    }
}

fun Route.authRoute(path: String, build: Route.() -> Unit) {
    route(path) {
        authenticate(optional = false) {
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNAUTHORIZED_ACCESS
                        )
                    )
                    finish()
                    return@intercept
                }
                proceed()
            }
            build()
        }
    }
}

fun ApplicationCall.getCurrentUserId(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
}

fun ApplicationCall.getCurrentUserIdSafe(): String {
    return getCurrentUserId() 
        ?: throw IllegalStateException("User ID not found in JWT token - this should not happen within authenticated routes")
}

fun ApplicationCall.getCurrentUserType(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("userType")?.asString()
}

fun ApplicationCall.getCurrentUserTypeSafe(): String {
    return getCurrentUserType() 
        ?: throw IllegalStateException("User type not found in JWT token - this should not happen within authenticated routes")
}