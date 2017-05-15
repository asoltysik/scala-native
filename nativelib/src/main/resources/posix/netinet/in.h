#include <netinet/in.h>
#include <inttypes.h>

#ifndef __NETINET_IN_H
#define __NETINET_IN_H

struct scalanative_in_addr {
    in_addr_t s_addr;
};

struct scalanative_in6_addr {
    uint8_t _s6_addr[16];
};

void scalanative_convert_in_addr(struct scalanative_in_addr *in,
                                 struct in_addr *out);
void scalanative_convert_in6_addr(struct scalanative_in6_addr *in,
                                  struct in6_addr *out);

#endif // __NETINET_IN_H
