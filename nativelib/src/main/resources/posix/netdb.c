#include "netdb.h"
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

void scalanative_convert_scalanative_addrinfo(struct scalanative_addrinfo *in,
                                              struct addrinfo *out) {
    out->ai_flags = in->ai_flags;
    out->ai_family = in->ai_family;
    out->ai_socktype = in->ai_socktype;
    out->ai_protocol = in->ai_protocol;
    out->ai_addrlen = in->ai_addrlen;
    if(in->ai_canonname == NULL) {
	out->ai_canonname = NULL;
    } else {
    	out->ai_canonname = strdup(in->ai_canonname);
    }
    if (in->ai_addr == NULL) {
        out->ai_addr = NULL;
    } else {
        struct sockaddr *converted_addr = malloc(in->ai_addrlen);
        socklen_t *len = malloc(sizeof(socklen_t));
        *len = in->ai_addrlen;
        scalanative_convert_sockaddr(in->ai_addr, &converted_addr, len);
        out->ai_addr = converted_addr;
        free(len);
    }
    if (in->ai_next != NULL) {
        struct addrinfo *converted = malloc(sizeof(struct addrinfo));
        scalanative_convert_scalanative_addrinfo(
            (struct scalanative_addrinfo *)in->ai_next, converted);
        out->ai_next = converted;
    } else {
        out->ai_next = NULL;
    }
}

void scalanative_convert_addrinfo(struct addrinfo *in,
                                  struct scalanative_addrinfo *out) {
    out->ai_flags = in->ai_flags;
    out->ai_family = in->ai_family;
    out->ai_socktype = in->ai_socktype;
    out->ai_protocol = in->ai_protocol;
    out->ai_addrlen = in->ai_addrlen;
    if(in->ai_canonname == NULL) {
	out->ai_canonname = NULL;
    } else {
    	out->ai_canonname = strdup(in->ai_canonname);
    }
    if (in->ai_addr == NULL) {
        out->ai_addr = NULL;
    } else {
        struct scalanative_sockaddr *converted_addr = malloc(in->ai_addrlen);
        socklen_t *len = malloc(sizeof(socklen_t));
        *len = in->ai_addrlen;
        scalanative_convert_scalanative_sockaddr(in->ai_addr, converted_addr,
                                                 len);
        out->ai_addr = converted_addr;
        free(len);
    }
    if (in->ai_next != NULL) {
        struct scalanative_addrinfo *converted =
            malloc(sizeof(struct scalanative_addrinfo));
        scalanative_convert_addrinfo(in->ai_next, converted);
        out->ai_next = converted;
    } else {
        out->ai_next = NULL;
    }
}

void scalanative_freeaddrinfo(struct scalanative_addrinfo *addr) {
	// We don't free the first pointer because it is passed from Scala Native
	addr = addr->ai_next;
	struct scalanative_addrinfo *current;
	while(addr != NULL) {
		free(addr->ai_addr);
		free(addr->ai_canonname);
		current = addr;
		addr = (struct scalanative_addrinfo *)addr->ai_next;
		free(current);
	}
}

int scalanative_getaddrinfo(char *name, char *service,
                            struct scalanative_addrinfo *hints,
                            struct scalanative_addrinfo *res) {
    struct addrinfo hints_converted;
    struct addrinfo *res_c;
    scalanative_convert_scalanative_addrinfo(hints, &hints_converted);
    int status = getaddrinfo(name, service, &hints_converted, &res_c);
    scalanative_convert_addrinfo(res_c, res);
    freeaddrinfo(res_c);
    return status;
}

int scalanative_getnameinfo(struct scalanative_sockaddr *addr,
                            socklen_t addrlen, char *host, socklen_t hostlen,
                            char *serv, socklen_t servlen, int flags) {
    struct sockaddr *converted_addr;
    scalanative_convert_sockaddr(addr, &converted_addr, &addrlen);
    int status = getnameinfo(converted_addr, addrlen, host, hostlen, serv,
                             servlen, flags);
    free(converted_addr);
    return status;
}
